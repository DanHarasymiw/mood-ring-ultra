import random, requests, time

url = 'http://178.128.224.20:7777/number'

def make_request():
    number = random.randint(1, 101)
    print('sending request')
    try:
        requests.post(url, data={'number': number})
        print('request successfully sent')
    except:
        print('request failed')
    print('request sent')

while True:
    make_request()
    time.sleep(5)
