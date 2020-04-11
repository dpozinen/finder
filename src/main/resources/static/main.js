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

function refreshTable(page) {
    var v = `
        <tr>
            <td>
                ${page.url}
            </td>
            <td>
                ${page.urlsFound}
            </td>
            <td>
                ${page.level}
            </td>
        </tr>
    `;
    $("#greetings").append(v);
}
