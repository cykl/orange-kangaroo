'use strict';

var stompClient = null;
var username = null;

function connect() {
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = () => {};
    stompClient.connect({}, onConnected, onError);
}

function setAuditStatus(msg) {
    var auditStatus = document.querySelector("#audit-status");
    auditStatus.textContent = msg;
}

function onConnected() {
    setAuditStatus("Ricardo Websocket connection established...");

    stompClient.subscribe('/topic/dbevents', onMessageReceived);
}

function onError(error) {
    setAuditStatus("Ricardo Websocket connection failed. Please refresh this page to try again!");
}

var auditLog = document.querySelector("#audit-log");
function addDbEvent(dbEvent) {
    var timestamp = Date.now();
    var text = `Timestamp: ${timestamp} ==> ${dbEvent.op} id: ${dbEvent.id}`;

    var li = document.createElement('li');
    li.appendChild(document.createTextNode(text));
    auditLog.appendChild(li);
}

function onMessageReceived(payload) {
    var dbEvent = JSON.parse(payload.body);

    addDbEvent(dbEvent);
}

connect();
