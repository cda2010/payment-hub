(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('StructureOrganisationFacturier', StructureOrganisationFacturier);

    StructureOrganisationFacturier.$inject = ['$resource','BASE_URL_API'];

    function StructureOrganisationFacturier ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'structure-organisation-facturiers/:id';

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
