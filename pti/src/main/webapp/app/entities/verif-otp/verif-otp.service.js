(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('VerifOtp', VerifOtp);

    VerifOtp.$inject = ['$resource'];

    function VerifOtp ($resource) {
        var resourceUrl =  'api/verif-otps/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
