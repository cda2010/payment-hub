(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('PrametreExport', PrametreExport);

    PrametreExport.$inject = ['$resource','BASE_URL_API'];

    function PrametreExport ($resource, BASE_URL_API) {
        var resourceUrl =  'api/prametre-exports/:id';

        return $resource(resourceUrl, {}, {
            'queryByExportTable': { method: 'GET', url:BASE_URL_API + 'prametre-exports/of-exporttable/:id', isArray: true},
            'query': { method: 'GET', isArray: true},
            'getOne': { method: 'GET', url:BASE_URL_API+'prametre-exports/:id'},
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
