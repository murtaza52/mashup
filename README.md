# Mashup Generator

The mashup generator is an application that can be used to view a
person's tweets and github events.

It retrieves and parses the tweets / events in parallel. The data is
then presented using a clojurescript application.

The data can also be grouped using either day, month or year. Only the
data is sent across the wire, while the html is generated on the client.

## Prerequisites

To run the application, please do the following -

1. Clone the repository.
2. Run lein deps.
3. Run lein cljsbuild once.
4. Run lein repl.

(leiningen 2 is assumed to be the default lein.)

`lein repl` starts the development server and serves the index page.

## Configuration

The configuration options can be specified in the config.clj

The sample configuration provided with the application is enough to run
it out of the box.

To undersatnd more about the application please visit the config.clj
section in the documentation.

## Documentation

The documentation is done using the excellent marginalia lib. The
generated documentation is avalaible on the [project page]
(http://murtaza52.github.com/mashup).

Please visit the documentation (http://murtaza52.github.com/mashup).

## Testing

The project uses the excellent midje library for writing and running
tests err... facts ! (The cljs code is not tested and midje doesnt
support it)

The test can be run using lein midje

## Dependencies

This project certainly stands on the shoulder of giants. A big shoutout
to the authors of the following libs-

- Ring - For making the clojure web ecosystem possible !
- Compojure - For creating a composable routing DSL.
- clj-oauth - For creating a sane oauth authentication lib.
- twitter-api - For providing a wrapper for the insanity that is twitter
  api.
- webfui - For releasing one of the best clojurescript app frameworks
  that I have seen. I really hope it makes it into the big league !
- clj-time - For publishing a deal saver when dealing with date and
  time !
- shoreleave and fetch - For making cljs remoting a piece of cake.
- domina - For providing a native way to manipulate the dom.
- cljsbuild - For making clojurescript compilation effortless.
- pedantic - For making it possible to discover conflicting dependencies.
- clojure.core - For developing clojure itself !!!

Open Source Rocks and Clojure Rocks Open Source !!!

## Acknowledgements

I did ask a lot of questions on stackoverflow, and am thankful to the
numerous authors who shared their knowledge with me !

Also a big thanks to the guys at

## Known Bugs

Yes even shiny new porjects have them -

1. The day button should be pressed when the page loads. Currently
unable to execute the js needed for this.
2. The sorting doesnt work. Unable to trace the reason at this point :(

## License

Copyright © 2013 Murtaza Husain

