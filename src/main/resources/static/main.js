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
        stompClient.subscribe('/updates/' + jobId, function (data) {
            var response = JSON.parse(data.body);
            addNewPages(response.newPages);
            updatePages(response.updatedPages);
//            updateTotal(response.pages.length);
        });
    });
}

function updatePages(pages) {
    $.each(pages, function(i, v) {
        var id = v.id;

        $('#'+id).find('.status').text(v.status);
    });
}

function addNewPages(pages) {
    var html = $.map(pages, function(page, i) {
        return `
            <tr id="${page.id}">
                <td class="url">
                    ${page.url}
                </td>
                <td class="level">
                    ${page.level}
                </td>
                <td class="status">
                    ${getStatus(page)}
                </td>
            </tr>
        `;
    }).join("");
    $('#pages').append(html);
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
