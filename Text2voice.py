import cv2
import numpy as np
import pytesseract
from PIL import Image
import re

# Import the required module for text
# to speech conversion
from gtts import gTTS

# This module is imported so that we can
# play the converted audio
import os

############################################################################################################
def get_string(image_name):
# Read image as grayscale image
    img = cv2.imread(image_name+".png",0)
# resize image
    scale_percent = 280  # percent of original size
    width = int(img.shape[1] * scale_percent / 100)
    height = int(img.shape[0] * scale_percent / 100)
    dim = (width, height)
    img = cv2.resize(img, dim, interpolation=cv2.INTER_AREA)
# Apply dilation and erosion to remove some noise
    kernel = np.ones((2, 2), np.uint8)
    for i in range(1,2):
        img = cv2.erode(img, kernel, iterations=i)
        img = cv2.dilate(img, kernel, iterations=i)

# Apply threshold
    ret,img = cv2.threshold(img,175,255,cv2.THRESH_BINARY)
# Write the image after apply opencv to do some ...
    cv2.imwrite(image_name+"after_noise_releasing.png", img)
# Recognize text with tesseract for python
    result = pytesseract.image_to_string(Image.open(image_name+"after_noise_releasing.png"))
    return result

if __name__ == "__main__":
# Print the string extracted from the image:
    image_name = input('please enter the name of the image: \n ')
    string = get_string(image_name) # here you write the name of the image file you want to convert
    print "Text is: \n",string

# Split the string into words:
    wordsList = re.findall('([a-zA-Z]*)\n*?', string)
    while True:
        try:
            wordsList.remove(u'')
        except:
            break
    print 'wordsList: \n', wordsList

# count the number of characters in each word
    wordsDict = dict()
    number_of_characters = 0
    number_of_words = 0
    for word in wordsList:
        if word not in wordsDict:
            wordsDict[word] = 1
        else:
            wordsDict[word] = wordsDict[word] + 1
            number_of_words = number_of_words + 1

    for k, v in wordsDict.items():
        number_of_characters = number_of_characters + v*len(k)

    number_of_words = len(wordsDict)+number_of_words
    print "number of words is: ", number_of_words
    print 'wordsDict : \n', wordsDict
    print "number of charachters is: ", number_of_characters
    L=list()
    L=[ "number of words is: ", str(number_of_words),"\nnumber of charachters is: ", str(number_of_characters)]
    file1=open("p10_output.txt","w")
    file1.writelines(L)
    file1.close()


    # The text that you want to convert to audio
    mytext = string
    # Language in which you want to convert
    language = 'en'
    # Passing the text and language to the engine,
    # here we have marked slow=False. Which tells
    # the module that the converted audio should
    # have a high speed
    myobj = gTTS(text=mytext, lang=language, slow=False)

    # Saving the converted audio in a mp3 file
    myobj.save(image_name+"_voice"+".mp3")

# Playing the converted file
os.system(image_name+"_voice"+".mp3")
