import cv2
import numpy as np
 
# 读取图片
image = cv2.imread('test.webp')
 
# 转换为灰度图
gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
cv2.imshow('Lane Detection', gray)
cv2.waitKey(0)
 
# 应用高斯模糊
# blur = cv2.GaussianBlur(gray, (5, 5), 0)

# binary
thresh, im = cv2.threshold(gray, 200, 255, cv2.THRESH_BINARY)
cv2.imshow('Lane Detection', im)
cv2.waitKey(0)
 
# 边缘检测
edges = cv2.Canny(im, 50, 150)
cv2.imshow('Lane Detection', edges)
cv2.waitKey(0)
 
# # 霍夫变换检测直线  
# lines = cv2.HoughLinesP(edges, 2, np.pi / 180, threshold=100, minLineLength=100, maxLineGap=10)
 
# # 绘制线条
# for line in lines:
#     x1, y1, x2, y2 = line[0]
#     cv2.line(image, (x1, y1), (x2, y2), (0, 255, 0), 2)
 
# # 显示图像
# cv2.imshow('Lane Detection', image)
# cv2.waitKey(0)
# cv2.destroyAllWindows()