function goToPage() {
    var pageInput = document.getElementById("pageInput").value;
    console.log("pageInput = ", pageInput);
    var maxPage = /*[[${page.getTotalPages()}]]*/ 1; // Default value

    // Extract current parameters from the URL
    var currentUrl = window.location.href;
    var pageSizeParam = getParameterByName("size", currentUrl);
    var sortFieldParam = getParameterByName("sortField", currentUrl);
    var sortDirectionParam = getParameterByName("sortDirection", currentUrl);

    // Build the new URL with current parameters
    var newUrl = '/admin/quote?page=' + pageInput;
    if (pageSizeParam !== null) {
        newUrl += '&size=' + pageSizeParam;
    }
    if (sortFieldParam !== null && sortDirectionParam !== null) {
        newUrl += '&sortField=' + sortFieldParam + '&sortDirection=' + sortDirectionParam;
    }

    // Perform the navigation
    window.location.href = newUrl;
}

// Helper function to get parameter value from URL
function getParameterByName(name, url) {
    name = name.replace(/[\[\]]/g, "\\$&");
    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

//for modal window with js
let modal =
    new bootstrap.Modal(document.getElementById('confirmDeleteModal'));

function showModal() {
    modal.show();
}

//for pagination
function changePageSize(select) {
    var selectedPageSize = select.value;
    var sortField = document.getElementById("sortField").value;
    var sortDirection = document.getElementById("sortDirection").value;

    // Обновление URL с параметрами сортировки и размера страницы
    var currentUrl = window.location.href;
    var updatedUrl = updateQueryStringParameter(currentUrl, "size", selectedPageSize);

    if (sortField.trim() !== "" && sortDirection.trim() !== "") {
        updatedUrl = updateQueryStringParameter(updatedUrl, "sortField", sortField);
        updatedUrl = updateQueryStringParameter(updatedUrl, "sortDirection", sortDirection);
    }

    window.location.href = updatedUrl;
}

//for sorting
function applySort() {
    var sortField = document.getElementById("sortField").value;
    var sortDirection = document.getElementById("sortDirection").value;
    console.log("sortField = ", sortField);

    // Обновление URL с параметрами сортировки
    var currentUrl = window.location.href;

    // Проверка, является ли sortField пустой строкой
    if (sortField.trim() !== "") {
        currentUrl = updateQueryStringParameter(currentUrl, "sortField", sortField);
        currentUrl = updateQueryStringParameter(currentUrl, "sortDirection", sortDirection);
    } else {
        // Если sortField пуст, удаляем параметры sortField и sortDirection из URL
        currentUrl = removeQueryStringParameter(currentUrl, "sortField");
        currentUrl = removeQueryStringParameter(currentUrl, "sortDirection");
    }

    // Удаление последнего символа & в конце URL (если есть)
    currentUrl = currentUrl.replace(/&$/, '');

    window.location.href = currentUrl;
}

// Вспомогательная функция для обновления параметров в URL
function updateQueryStringParameter(uri, key, value) {
    var re = new RegExp("([?&])" + key + "=.*?(&|$)", "i");
    var separator = uri.indexOf("?") !== -1 ? "&" : "?";
    if (uri.match(re)) {
        return uri.replace(re, "$1" + key + "=" + value + "$2");
    } else {
        return uri + separator + key + "=" + value;
    }
}

// Вспомогательная функция для удаления параметра из URL
function removeQueryStringParameter(uri, key) {
    var re = new RegExp("([?&])" + key + "=.*?(&|$)", "i");

    if (uri.match(re)) {
        return uri.replace(re, "$1$2").replace(/&$/, '');
    } else {
        return uri;
    }
}