/*Добавление скрипта для AJAX-запроса и обработки loader-spinner'а*/

function scanBooks() {
    /*Отображаем loader-spinner перед отправкой запроса*/
    document.getElementById("loader").style.display = "block";

    /*Отключаем кнопку "Сканировать каталог"*/
    document.getElementById("scanButton").disabled = true;

    /*Отправка формы с использованием Fetch API*/
    var form = document.getElementById('scanForm');
    var formData = new FormData(form);

    fetch(form.action, {
        method: form.method,
        body: formData
    })
        .then(response => response.text())
        .then(data => {
            /*Скрываем loader-spinner после получения ответа*/
            document.getElementById("loader").style.display = "none";

            /*Вставляем ответ от сервера внутрь элемента с id "result-container"*/
            document.getElementById("result-container").innerHTML = data;

            /*Включаем кнопку "Сканировать каталог" после завершения сканирования*/
            document.getElementById("scanButton").disabled = false;
        })
        .catch(error => {
            console.error('Ошибка:', error);
            /*Скрываем loader-spinner в случае ошибки*/
            document.getElementById("loader").style.display = "none";

            /*Включаем кнопку "Сканировать каталог" в случае ошибки*/
            document.getElementById("scanButton").disabled = false;
        });
}