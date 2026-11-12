(function() {
    'use strict';
    angular
        .module('omnicanalApp')
        .factory('Pays', Pays);

    Pays.$inject = ['$resource'];

    function Pays ($resource) {
        var resourceUrl =  'api/pays/:id';

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
