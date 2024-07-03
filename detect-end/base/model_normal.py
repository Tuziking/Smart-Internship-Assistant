from ultralytics import YOLOv10

class Model:

    '''
        construction
        params: model_name, model_path
        output: none
        
    '''
    def __init__(self, model_name, model_path='model') -> None:
        # self.model = YOLOv10('epoch80.pt') # init the yolov10 model
        self.model = YOLOv10(model_path+"/"+model_name+".pt") # init the yolov10 model
        pass

    '''
        function predict
        params: input img(cv2 format)
        output: list
        {
            className:[...],
            boxes:[...],
            speed:time,
            plot:return img
        }

        
    '''
    def predict(self, img):
        results = self.model(source=img, conf=0.05, verbose=False)  # return a list of Results objects

        # result
        className=[]
        boxes=[]
        spd=0

        i=0
        # analysis the result
        for result in results:
            i+=1    
            # print("--------------------------------------------------------")

            # print("boxes:")
            # print(result.boxes.xyxy)
            boxes.append(result.boxes.xyxy)
            # print("")

            # print("className:")
            # print(result.boxes.cls)
            names = result.names
            classes=result.boxes.cls
            # print("test:")
            # print(names)  
            # print(int(list(classes)[0]))
            for i in range(0,len(list(classes))):
                className.append(names[int(list(classes)[i])])  
            # print("className:")
            # print(className)
            # print("") 

            speed = result.speed
            # print("speed:")
            # print(speed)
            spd+=speed['preprocess']+speed['inference']+speed['postprocess']
            # print("")

            # print("names:")
            # print(names)

            # print("")

            # path = result.obb
            # print("speed:"+path)

            # probs = result.probs
            # print("probs:")
            # print(probs)

            # result.show()  # display to screen      
            # result.save(filename="result/img"+str(i)+".jpg")  # save to disk
    
            # print("--------------------------------------------------------")
        
        # return
        return {
            'className':className,
            'boxes':boxes,
            'speed':spd,
            'spot':results[0].plot()
        }