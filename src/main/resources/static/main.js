var stompClient = null;

function prepare() {
    connect();
}

function connect() {
    var socket = new SockJS('/finder/stream');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/updates', function (table) {
            refreshTable(JSON.parse(table.body));
        });
    });
}

function refreshTable(pages) {
    var html = $.map(pages, function(page, i) {
        return `
            <tr>
                <td>
                    ${page.url}
                </td>
                <td>
                    ${page.level}
                </td>
                <td>
                    ${getStatus(page)}
                </td>
            </tr>
        `;
    }).join("");
    $('#pages').html(html);
}

function getStatus(page) {
    var status = page.status;
    if (status == 'ERROR') {
        return page.errorMsg;
    } else {
        return status;
    }
}
