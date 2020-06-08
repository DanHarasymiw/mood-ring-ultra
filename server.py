numbers = [0, 100]

from bottle import post, request, route, run, template

webpage = '''
<!DOCTYPE html>
<html>
<body>
    <h1 style="width:250px;
               height:250px;
               border-radius:50%;
               line-height:250px;
               text-align:center;
               background-color:{{colour}}">Mood Ring Ultra
   </h1>
</body>
</html>
'''

@route('/')
def hello():
    max_num = max(numbers)
    min_num = min(numbers)
    score = 100 - (max_num - min_num)
    print(score)
    if score > 95:
        colour = "blue"
    elif score > 90:
        colour = "cyan"
    elif score > 40:
        colour = "green"
    elif score > 10:
        colour = "yellow"
    elif score > 5:
        colour = "orange"
    else:
        colour = "red"

    # return template('The mood ring is {{colour}}', colour=colour)
    return template(webpage, colour=colour)

@post('/number')
def number():
    num = int(request.forms.get('number'))
    if num > 100 or num < 1:
        return

    if len(numbers) >= 3:
        numbers.pop(0)
    numbers.append(num)

run(host='localhost', port=7777, debug=True)
