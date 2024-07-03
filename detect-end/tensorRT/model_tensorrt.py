class Model:

    def __init__(self) -> None:
        self.model = YOLOv10('epoch40.pt') # init the yolov10 model
        pass

    def predict(self):
        results = self.model(conf=0.05)  # return a list of Results objects

        i=0
        # analysis the result
        for result in results:
            i+=1    
            print("--------------------------------------------------------")

            print("boxes:")
            print(result.boxes.xyxy)

            print("")

            print("className:")
            print(result.boxes.cls)

            print("")

            # masks = result.masks  # Masks object for segmentation masks outputs
            # print("masks:"+masks)

            # keypoints = result.keypoints  # Keypoints object for pose outputs
            # print("keypoints:"+keypoints)

            # probs = result.probs  # Probs object for classification outputs
            # print("probs:"+probs)

            # obb = result.obb  # Oriented boxes object for OBB outputs
            # print("obb:"+obb)

            speed = result.speed
            print("speed:")
            print(speed)

            print("")

            names = result.obb
            print("names:")
            print(names)

            print("")

            # path = result.obb
            # print("speed:"+path)

            result.show()  # display to screen  
            result.save(filename="res/result"+str(i)+".jpg")  # save to disk

            print("--------------------------------------------------------")
        pass