TODO
 - HW1
    ?????

 - HW2
    add parallelism in FileProcessor, batching by i.e. 1000 records per thread
    У тебя получилось решение, которое сильно зависит от входных данных – даже не только объема, но и состава.
    Если там будет меньше повторов например – уже и 8гб памяти перестанет хватать 
    Я бы предложил подумать на тему того, как сделать его (почти) независимым от инпута.

 - HW3
    По поводу списка браузеров – можно воспользоваться browser.getGroup чтобы получить нужный браузер – вместо списка всех
    Пропущенные записи (без байтов) я бы предложил не добавлять к счетчикам браузеров, а вывести контер «BadRecords» или что-то в этом духе (насколько я помню там есть какой-то встроенный даже)

    По поводу комбайнера:
    Строго говоря, правильнее было бы выводить не (avg, total) а (count, total), и среднее считать уже только в редьюсере.
    Так не придется делать лишние вычисления + выше точность 
     Если будет время-желание, попробуй перевести на такой вариант (не обязательно)

 - HW4
    filter by bid price field in mapper (or, as other variant, in reducer)
    choose 20th commit as right decision for partitioning
    remake "compareTo" method in OSTypeCityIdWritable, for sequential comparing
    DONE: get list of city-to-name map in "setup" method,
     TRY: or inner map just plain copy-&-paste in java plain code

 - HW5
    ?????