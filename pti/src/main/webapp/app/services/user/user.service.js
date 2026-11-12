(function () {
    'use strict';

    angular
        .module('ptiApp')
        .factory('User', User)
        .factory('InitPassword',InitPassword);

    User.$inject = ['$resource','BASE_URL_API'];
    InitPassword.$inject = ['$resource','BASE_URL_API'];

    function User ($resource,BASE_URL_API) {
        var service = $resource(BASE_URL_API+'users', {}, {
             'queryId': {method: 'GET', url:BASE_URL_API+'users/login/:id', isArray: false},
             'getByLogin': {method: 'GET', url:BASE_URL_API+'users/:login', isArray: false},
            'query': {method: 'GET', isArray: true},

            'getTempPass': {method: 'GET', url: BASE_URL_API+'users/temps_pass/:data', isArray: false},

            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    return data;
                }
            },
            'save': { method:'POST' },
            'update': { method:'PUT' },
            'delete':{ method:'DELETE'}
        });

        return service;
    }

    function InitPassword ($resource,BASE_URL_API) {
        var service = $resource(BASE_URL_API+'users/initPassword', {}, {
            'save': { method:'POST' }
        });
        return service;
    }
})();
