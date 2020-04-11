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

function sendName() {
    stompClient.send("/app/hello", {}, $("#name").val() );
}

function refreshTable(message) {
    var v = `
        <tr>
            <td>
                LOOOOL
            </td>
            <td>
                LOOOOL
            </td>
            <td>
                LOOOOL
            </td>
        </tr>
    `;
    $("#greetings").append(v);
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { sendName(); });
});
