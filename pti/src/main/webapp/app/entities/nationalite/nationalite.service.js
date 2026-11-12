(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Nationalite', Nationalite);

    Nationalite.$inject = ['$resource','BASE_URL_API'];

    function Nationalite ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'nationalites/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
              'queryAll': { method: 'GET',url: BASE_URL_API+'listnationalites' ,isArray: true},
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
