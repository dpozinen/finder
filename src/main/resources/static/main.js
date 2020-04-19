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
        if (page.errorMsg == null) {
            return page.statusCode;
        } else {
            return page.errorMsg;
        }
    } else {
        return status;
    }
}

function sendPause() {
    stompClient.send("/finder/pause", {}, "");
}

function sendStop() {
    stompClient.send("/finder/stop", {}, "");
}

function sendPlay() {
    stompClient.send("/finder/play", {}, "");
}

function sendReset() {
    stompClient.send("/finder/reset", {}, "");
}
