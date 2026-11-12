(function () {
  var log = console.log;
  console.log = function (msg) {
    //On désactive tous les log
    log.apply(console, arguments);
  };

})();