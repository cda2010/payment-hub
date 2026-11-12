(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('RecepteurEmail', RecepteurEmail);

    RecepteurEmail.$inject = ['$resource'];

    function RecepteurEmail ($resource) {
        var resourceUrl =  'api/recepteur-emails/:id';

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
