(function() {
    'use strict';
    angular
        .module('omnicanalApp')
        .factory('TypeUniteOrganisation', TypeUniteOrganisation);

    TypeUniteOrganisation.$inject = ['$resource'];

    function TypeUniteOrganisation ($resource) {
        var resourceUrl =  'api/type-unite-organisations/:id';

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
