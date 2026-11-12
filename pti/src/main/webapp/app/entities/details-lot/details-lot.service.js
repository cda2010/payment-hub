(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('DetailsLot', DetailsLot);

    DetailsLot.$inject = ['$resource','BASE_URL_API'];

    function DetailsLot ($resource,BASE_URL_API) {
        var resourceUrl =  'api/details-lots/:id';

        return $resource(resourceUrl, {}, {
            'queryByLot': { method: 'GET', url:BASE_URL_API + 'details-lots/of-lot/:id', isArray: true},
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
