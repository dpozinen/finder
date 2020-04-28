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
        checkStatus();
        var v = stompClient.subscribe('/updates/' + jobId, function (data) {
            var response = JSON.parse(data.body);
            addNewPages(response.newPages);
            updatePages(response.updatedPages);
        });
    });
}

function checkStatus() {
    $.ajax({
        url: '/job?id=' + jobId,
        method: 'get',
        success : function (data) {
            if (data.status == 'DONE') {
                fixButtons([], ['#play', '#pause', '#stop']);
                stompClient.disconnect(function() {
                    $.ajax({
                        url: '/results/job?id=' + jobId,
                        method: 'get',
                        success : function (data) {
                            $.each(data.pages, function(i, page) {
                                    var html = $.map(pages, makeRow(page, i)).join("");
                                    $('#pages').html(html);
                                });
                        }
                    });
                }, {});
            } else if (data.status == 'PAUSED') {
                fixButtons(['#play'], ['#pause']);
            } else if (data.status == 'CANCELLED') {
                fixButtons([], ['#play', '#pause', '#stop']);
            }
        },
        error : function(data) {
            var v = "";
        }
    })
}

function updatePages(pages) {
    $.each(pages, function(i, v) {
        var id = v.id;

        $('#'+id).find('.status').text(v.status);
    });
}

function addNewPages(pages) {
    var html = $.map(pages, function(v, i) { return makeRow(v, i) }).join("");
    $('#pages').append(html);
}

function makeRow(page, i) {
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

function getPagesIfDone() {
    stompClient.send("/finder/job/"+jobId, {}, "");
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
