(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('FacturierProfil', FacturierProfil);

        FacturierProfil.$inject = ['$resource'];

    function FacturierProfil ($resource) { 
        var resourceUrl =  'api/facturierProfil/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET' ,isArray: true},
            'queryAll': { method: 'GET',url:'api/facturierProfil' ,isArray: true},
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
