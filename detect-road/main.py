import cv2 ###opencv4.7
import numpy as np
import os
import argparse

class Ultra_Fast_Lane_Detection_v2:
    def __init__(self, modelpath):
        self.net = cv2.dnn.readNet(modelpath)
        input_shape = os.path.splitext(os.path.basename(modelpath))[0].split('_')[-1].split('x')
        self.input_height = int(input_shape[0])
        self.input_width = int(input_shape[1])

        dataset = os.path.basename(modelpath).split('_')[1]
        if dataset == 'culane':
            num_row = 72
            num_col = 81
            self.row_anchor = np.linspace(0.42, 1, num_row)
            self.col_anchor = np.linspace(0, 1, num_col)
            self.train_width = 1600
            self.train_height = 320
            self.crop_ratio = 0.6
        else:
            num_row = 56
            num_col = 41
            self.row_anchor = np.linspace(160, 710, num_row) / 720
            self.col_anchor = np.linspace(0, 1, num_col)
            self.train_width = 800
            self.train_height = 320
            self.crop_ratio = 0.8
        self.mean_ = np.array([0.485, 0.456, 0.406], dtype=np.float32).reshape((1, 1, 3))
        self.std_ = np.array([0.229, 0.224, 0.225], dtype=np.float32).reshape((1, 1, 3))

    def softmax(self, x, axis=0):
        exp_x = np.exp(x)
        return exp_x / np.sum(exp_x, axis=axis)
    def pred2coords(self, pred, local_width=1, original_image_width=1640, original_image_height=590):
        exist_col, exist_row, loc_col, loc_row = pred
        batch_size, num_grid_row, num_cls_row, num_lane_row = loc_row.shape
        batch_size, num_grid_col, num_cls_col, num_lane_col = loc_col.shape

        max_indices_row = loc_row.argmax(axis=1)
        # n , num_cls, num_lanes
        valid_row = exist_row.argmax(axis=1)
        # n, num_cls, num_lanes

        max_indices_col = loc_col.argmax(axis=1)
        # n , num_cls, num_lanes
        valid_col = exist_col.argmax(axis=1)
        # n, num_cls, num_lanes

        coords = []

        row_lane_idx = [1, 2]
        col_lane_idx = [0, 3]

        for i in row_lane_idx:
            tmp = []
            if valid_row[0, :, i].sum() > num_cls_row / 2:
                for k in range(valid_row.shape[1]):
                    if valid_row[0, k, i]:
                        all_ind = np.array(range(max(0, max_indices_row[0, k, i] - local_width), min(num_grid_row - 1, max_indices_row[0, k, i] + local_width) + 1))

                        out_tmp = (self.softmax(loc_row[0, all_ind, k, i], axis=0) * all_ind.astype(np.float32)).sum() + 0.5
                        out_tmp = out_tmp / (num_grid_row - 1) * original_image_width
                        tmp.append((int(out_tmp), int(self.row_anchor[k] * original_image_height)))
                coords.append(tmp)

        for i in col_lane_idx:
            tmp = []
            if valid_col[0, :, i].sum() > num_cls_col / 4:
                for k in range(valid_col.shape[1]):
                    if valid_col[0, k, i]:
                        all_ind = np.array(range(max(0, max_indices_col[0, k, i] - local_width), min(num_grid_col - 1, max_indices_col[0, k, i] + local_width) + 1))

                        out_tmp = (self.softmax(loc_col[0, all_ind, k, i], axis=0) * all_ind.astype(np.float32)).sum() + 0.5

                        out_tmp = out_tmp / (num_grid_col - 1) * original_image_height
                        tmp.append((int(self.col_anchor[k] * original_image_width), int(out_tmp)))
                coords.append(tmp)
        return coords

    def detect(self, srcimg):
        img_h, img_w = srcimg.shape[:2]
        img = cv2.resize(srcimg, (self.train_width, int(self.train_height / self.crop_ratio)))
        img = (img.astype(np.float32) / 255.0 - self.mean_) / self.std_
        img = img[-self.train_height:, :, :]
        blob = cv2.dnn.blobFromImage(img)
        self.net.setInput(blob)
        pred = self.net.forward(self.net.getUnconnectedOutLayersNames())
        coords = self.pred2coords(pred, original_image_width=img_w, original_image_height=img_h)
        return coords

if __name__=='__main__':

    net = Ultra_Fast_Lane_Detection_v2('weights/ufldv2_culane_res34_320x1600.onnx')

    cap = cv2.VideoCapture('images/culane/test.mp4')
    if not cap.isOpened():
        print("Error: Could not open video.")
        exit()
    winName = 'Deep learning lane detection in OpenCV'

    sign=0

    while(True):
        ret, frame = cap.read()
        if ret:
            coords = net.detect(frame)

            size = frame.shape
            width=size[1]/7
            height=9*size[0]/10
            center=(int(size[1]/2-width),int(height))
            cv2.circle(frame, center, 3, (255, 255, 255), -1)
            center=(int(size[1]/2+width),int(height))
            cv2.circle(frame, center, 3, (255, 255, 255), -1)

            for lane in coords:
                for coord in lane:
                    cv2.circle(frame, coord, 3, (0, 255, 0), -1)

                    # print(coord)
                    if coord[1]>height and coord[1]<size[0] and abs(coord[0]-size[1]/2)<width:
                        if sign==0:
                            print("偏离")
                        sign=15
                        font = cv2.FONT_HERSHEY_SIMPLEX
                        cv2.putText(frame,'out of lane',(10,100),font,1,(0,255,0))
            if sign>0:
                sign-=1
                if sign==0:
                    print("偏离解除\n")
            cv2.imshow(winName, frame)
            cv2.waitKey(1)
