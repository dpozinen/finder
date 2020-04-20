var stompClient = null;
var jobId = null;

function prepare() {
    connect();
}

function connect() {
    var urlParams = new URLSearchParams(window.location.search);

    jobId = urlParams.get('job');
    if (jobId == null) {
        fixButtons([], ['#play', '#pause', '#stop']);
        alert("No Job Id");
    }

    var socket = new SockJS('/finder/stream');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/updates/' + jobId, function (table) {
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
    stompClient.send("/finder/pause/"+jobId, {}, "");
}

function sendStop() {
    stompClient.send("/finder/stop/"+jobId, {}, "");
}

function sendPlay() {
    stompClient.send("/finder/play/"+jobId, {}, "");
}

function sendReset() {
    stompClient.send("/finder/reset/"+jobId, {}, "");
}


function fixButtons(toEnable, toDisable) {
    toEnable.forEach(e => $(e).prop('disabled', false));
    toDisable.forEach(e => $(e).prop('disabled', true));
}
