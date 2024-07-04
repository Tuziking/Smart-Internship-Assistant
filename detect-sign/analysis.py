from base.model_normal import Model
from kafka import KafkaProducer
import json
import time
import cv2

class Analysis:
    def __init__(self) -> None:
        self.dic={}
        self.m = Model('epoch160')

        self.producer = KafkaProducer(bootstrap_servers=['8.140.250.103:9092'])
        message = {'uID': 1, 
                    'code': 'Hello, Kafka!',
                    'timestamp':int(time.time() * 1000),
                    'msg':'null'}
        
        bytesDict = bytes('{}'.format(message),'utf-8')

        self.producer.send('sign', bytesDict)
        self.producer.flush()

        pass

    def predict(self,img):
        return self.m.predict(img)

    def update(self, list) -> list :
        # show
        cv2.imshow("YOLOv10 Inference", list['spot'])

        # update
        for i in range(0,len(list['className'])):
            # check out
            if list['className'][i] in self.dic:
                self.dic[list['className'][i]]+=1
                pass
            else:
                self.dic[list['className'][i]]=1
                pass

        # analysis
        res=[]
        for key in self.dic:
            # print(key, self.dic[key])
            if self.dic[key]>2:
                res.append(key)

        # clear
        for key in self.dic:
            if self.dic[key]>0:
                self.dic[key]-=1
            if self.dic[key]<0:
                self.dic[key]=0
        # print(self.dic)
        return res
    
    def run(self) -> None:

        cap = cv2.VideoCapture('video/test.mp4')
        if not cap.isOpened():
            print("Error: Could not open video.")
            exit()

        cate=[]
        number=0
        while(True):
            ret, frame = cap.read()
            if ret:
                # cv2.imshow('Video', frame)
                number+=1
                res=self.update(self.predict(img=frame))
                difference = list(set(res) - set(cate))

                if len(difference)>0:
                    print("在第"+str(number)+"轮中，检测到：",end="")
                    print(difference)

                    # send to Kafka
                    message = {'uID': 1, 
                            'code': json.dumps(difference),
                            'timestamp':int(time.time() * 1000),
                            'msg':'null'}
                    
                    bytesDict = bytes('{}'.format(message),'utf-8') 
                    self.producer.send('sign', bytesDict)
                    self.producer.flush()

                cate=list(set(cate+res))
                # print(cate)
        
                # 等待键盘输入，这里设置了1ms的延时
                # 如果按下q键，则退出循环
                if cv2.waitKey(1) & 0xFF == ord('q'):
                    break
            else:
                print("Error: Could not read frame.")
                break
            


if __name__== "__main__" :
    ans=Analysis()
    ans.run()

