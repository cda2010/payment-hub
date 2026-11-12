(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Facturier', Facturier);

    Facturier.$inject = ['$resource'];

    function Facturier ($resource) {
        var resourceUrl =  'api/facturiers/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'queryOptimiser': { method: 'GET',url :'api/facturiers-optimiser' ,isArray: true},
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
