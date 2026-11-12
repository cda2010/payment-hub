(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('BlackList', BlackList);

    BlackList.$inject = ['$resource'];

    function BlackList ($resource) {
        var resourceUrl =  'api/black-lists/:id';

        return $resource(resourceUrl, {}, {
            'query': {method: 'GET', isArray: true},
            'queryAll': { method: 'GET',url: '/api/queryAll' , isArray: true},
            'findByCode': {method: 'GET', url: '/api/findByCode/:id', isArray: true},
            'findByTel': {method: 'GET',url: '/api/findByTel/:id', isArray: true},
            'findByCodeAndTel' :{method: 'GET', url: '/api/findByCodeAndTel/:id1/:id2', isArray: true},
            'findClientBlackList' :{method: 'GET', url: '/api/black-liste-client', isArray: true},
            'delate': {method: 'delete', url: '/api/black-lists/:id'},
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
