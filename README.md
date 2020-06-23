# finder
Tiny crawler, that's supposed to find links, visit them (up to a limit) and look for a certain string.
The status and links found are displayed live. <br>
The requests are executed in an executor with a provided pool size. <br>
Supports pauses of jobs. During a pause active requests are finished, but new ones are not executed and parsing is paused.

##### current status
It all worked well unitl a move to Reddis pubsub from in-memory was made, at which point a redesign began and never ended.
