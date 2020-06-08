import random, requests, time

url = 'http://localhost:7777/number'

def make_request():
    number = random.randint(1, 101)
    try:
        requests.post(url, data={'number': number})
    except:
        print('request failed')
    print('request sent')

while True:
    make_request()
    time.sleep(600)
