(ns mashup.config
  (:use [midje.sweet :only [facts fact]]))

;; ### Configuration
;; This ns defines the config elements needed to connect to the
;; services.

;; #### Twitter Configuration
;; The twitter configuration can be obtained by registering a new
;; application from the link http://twitter.com/oauth_clients/new.

;; (I know this is unwieldly, so it is a todo to find an easier way to
;; read tweets, maybe just using the twitter handle etc. )

;; Sample (fully functional) twitter configuration is provided here.
;; (These keys have read only access, thus safe to be shared)

(def tw-consumer-key "PevL5bFkmtjOfg1zqHbSw")
(def tw-consumer-secret "p3oCRqVcqaoCyHoSqItKDFha0e1GdJ5EAXyjNkoDyI")
(def tw-access-token "158268144-dgk6wa3e2gyZZBkt8t7YEPkPatrWqIb64VHi4DHU")
(def tw-access-secret "cEHAgVxpSzx5nehH2rSdBrf97vNJURui5GKY5azdkPQ")

;; The twitter sceen name is only for display purposes, and any label,
;; but preferably your twiiter handle should be provided.

(def tw-screen-name "@rampurawala52")

;; #### Github Configuration
;; The github user name is all that needs to be provided. The read only
;; api is used to retreive the github events. A client-id and
;; secret-key will be required if operations other than read ones are to
;; be performed on github.

(def github-user-name "murtaza52")

(facts "Ensure all the required config exists."
       (fact "twitter consumer key exists"
             tw-consumer-key => string?)
       (fact "twitter consumer secret exists"
             tw-consumer-secret => string?)
       (fact "twitter access token exists"
             tw-access-token => string?)
       (fact "twitter access secret exists"
             tw-access-secret => string?)
       (fact "twitter screen name exists"
             tw-screen-name => string?)
       (fact "github user name exists"
             github-user-name => string?))


















































