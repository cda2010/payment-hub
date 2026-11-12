(function () {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Machine', Machine);

    Machine.$inject = ['$resource','BASE_URL_API'];

    function Machine($resource,BASE_URL_API) {
        var resourceUrl = BASE_URL_API+'machines/:id';

        return $resource(resourceUrl, {}, {
            'query': {method: 'GET', isArray: true},
            'allow': {method: 'GET', url: BASE_URL_API+'activeMachine',isArray: false},
            'updateMachine': {method: 'PUT', url: BASE_URL_API+'machine-updated',isArray: false},
            'deleteMachine':{method: 'DELETE', url:BASE_URL_API+'machines/delete',isArray: false },
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': {method: 'PUT'}
        });
    }
})();
