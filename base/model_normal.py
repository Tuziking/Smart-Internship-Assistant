from ultralytics import YOLOv10

class Model:

    def __init__(self) -> None:
        # self.model = YOLOv10('epoch80.pt') # init the yolov10 model
        self.model = YOLOv10('epoch80.pt') # init the yolov10 model
        pass

    '''
        function predict
        params: input img(cv2 format)
        output: list
        [
            {
                className:[...],
                boxes:[...],
                speed:time
            }
        ]
        
    '''
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
            result.save(filename="result/img"+str(i)+".jpg")  # save to disk

            print("--------------------------------------------------------")
        pass