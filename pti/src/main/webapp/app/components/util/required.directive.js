(function() {
    "use strict";
    angular.module("ptiApp").directive('mfRequired', [function() {
        return {
            restrict: 'A',
            link: function(scope, iElement, iAttrs) {
                angular.element(iElement).ready(function() {
                    iElement.append("<span class='text-danger'><b> *</b></span>");
                    iElement.addClass("pr2");
                });
            }
        };
    }]);
})();