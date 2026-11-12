(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('TableauFrais', TableauFrais);

    TableauFrais.$inject = ['$resource','BASE_URL_API'];

    function TableauFrais ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'tableau-frais/:id';

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
