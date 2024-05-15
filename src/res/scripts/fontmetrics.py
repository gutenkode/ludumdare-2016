from PIL import Image
import struct

numChars = (16,16)
image = Image.open('img.png')
pixels = image.load()

charSize = (image.size[0]/numChars[0], image.size[1]/numChars[1])
print('Char size: ', charSize)

if charSize[0] != int(charSize[0]) or charSize[1] != int(charSize[1]):
    raise ValueError('Character size is not an integer value.')

def getCharWidth(x, y):
    lastColumn = 0
    for w in range(int(charSize[0])):
        for h in range(int(charSize[1])):
            if pixels[charSize[0]*x+w, charSize[1]*y+h][3] > 0: # if the pixel's alpha is >0
                lastColumn = w+1 # since w starts at 0
    if lastColumn == 0:
        lastColumn = int(charSize[0])
    else:
        lastColumn += 1 # +1 for a row of whitespace padding
        lastColumn = min(lastColumn, charSize[0])
    return int(lastColumn)

charWidths = [[getCharWidth(x,y) for x in range(numChars[0])] for y in range(numChars[1])]

# special case for setting the width of the space character
charWidths[2][0] = int(charSize[0]*.35);

print('Generated character metrics:')
for list in charWidths:
    print(list)

file = open('./out.metric', 'wb')
for list in charWidths:
            file.write(bytes(list)) # write all character metrics
file.write(bytes([int(charSize[0]),int(charSize[1])])); # write the dimensions of characters in this metric
file.close()
