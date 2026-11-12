(function() {
    'use strict';
    angular
        .module('omnicanalPresentationApp')
        .factory('Pays', Pays);

    Pays.$inject = ['$resource'];

    function Pays ($resource) {
        var resourceUrl =  'api/pays/:id';

        return $resource(resourceUrl, {}, {
            'appelApi': { method: 'POST', url:'http://localhost:6980/api/virementsMultiple', isArray: false},
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
