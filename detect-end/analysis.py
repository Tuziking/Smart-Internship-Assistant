from base.model_normal import Model
from kafka import KafkaProducer
import json
import time

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

    def predict(self):
        return self.m.predict()

    def update(self, list) -> list :

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
            print(key, self.dic[key])
            if self.dic[key]>2:
                res.append(key)

        # clear
        for key in self.dic:
            if self.dic[key]>0:
                self.dic[key]-=1
            if self.dic[key]<0:
                self.dic[key]=0

        return res
    
    def run(self) -> None:
        cate=[]

        number=0
        # while(True):
        number+=1
        res=self.update(self.predict())
        difference = list(set(cate) - set(res)) + list(set(res) - set(cate))

        if len(difference)>0:
            print("在第"+str(number)+"轮中，检测到：",end="")
            print(difference)

            # send to Kafka
            message = {'uID': 1, 
                       'code': json.dumps(difference),
                       'timestamp':time.time,
                        'msg':'null'}
            self.producer.send('sign', message)
            self.producer.flush()

        cate=list(set(cate) | set(res))


if __name__== "__main__" :
    ans=Analysis()
    ans.run()

