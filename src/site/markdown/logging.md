Logging
=======

The log format is as follows:

    %d{yyyy-MM-dd' 'HH:mm:ss.SSSZ} [%8.8X{X-Request-ID}] %40.40logger{40} %.-1level %msg%n
    
An example output is 

    2017-10-14 00:04:09.111+0000 [        ] n.trajano.ms.example.authz.SampleAuthzMS I Started SampleAuthzMS in 29.161 seconds (JVM running for 34.793)
    2017-10-14 00:05:11.984+0000 [nJtIg1m1]    n.t.m.c.oauth.AllowAnyClientValidator W AllowAnyClientValidator is being used
    
The layout ensures the non-message components will always line up.

The columns are:

* timestamp (which includes TimeZone to prevent ambiguity)
* the value of the `X-Request-ID` header if available.
* a shortened version of the logger name
* the log level initial (_E_rror, _W_arning, _I_nfo, _D_ebug, _T_race)

### FAQ

* Why is thread ID not added?

   Thread ID is primarily used for lower level debugging, rather than operations level logging.  The request ID column provides the unique request ID that flows through the application.