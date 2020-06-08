import random, requests, time

url = 'http://moodringultra.ca:7777/number'

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
