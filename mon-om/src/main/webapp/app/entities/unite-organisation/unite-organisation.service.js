(function() {
    'use strict';
    angular
        .module('omnicanalApp')
        .factory('UniteOrganisation', UniteOrganisation);

    UniteOrganisation.$inject = ['$resource'];

    function UniteOrganisation ($resource) {
        var resourceUrl =  'api/unite-organisations/:id';

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
