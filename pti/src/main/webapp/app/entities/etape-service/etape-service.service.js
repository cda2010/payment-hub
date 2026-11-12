(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('EtapeService', EtapeService);

        EtapeService.$inject = ['$resource'];

    function EtapeService ($resource) { 
        var resourceUrl =  'api/etapeService/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET' ,isArray: true},
            'queryAll': { method: 'GET',url:'api/etape_service' ,isArray: true},
            //'queryByType': { method: 'GET',url:'api/service-profils/byType/:type', isArray: true},
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
