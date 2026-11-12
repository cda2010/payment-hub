(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Lot', Lot);

    Lot.$inject = ['$resource','BASE_URL_API'];

    function Lot ($resource,BASE_URL_API) {
        var resourceUrl =  'api/lots/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'queryByType': { method: 'GET',isArray: true},
            'export': { method: 'POST', url: BASE_URL_API+'etat/lots/export', isArray: false},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' },
            'setEtat': { method:'PUT', url:BASE_URL_API+'lots/set-etat', isArray: false}
        });
    }
})();
