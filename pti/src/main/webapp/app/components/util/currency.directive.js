(function() {
    "use strict";
    angular.module("ptiApp").directive('format', ['$filter',function($filter) {
        return {
            require: '?ngModel',
            link: function (scope, elem, attrs, ctrl) {
                if (!ctrl) return;
                ctrl.$formatters.unshift(function (a) {
                    return $filter(attrs.format)(ctrl.$modelValue)
                });
                elem.bind('blur', function(event) {
                    var plainNumber = elem.val();
                    elem.val($filter(attrs.format)(plainNumber));
                });
            }
        };
    }]);
})();