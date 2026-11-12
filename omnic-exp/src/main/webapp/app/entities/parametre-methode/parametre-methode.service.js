(function() {
    'use strict';
    angular
        .module('omnicanalPresentationApp')
        .factory('ParametreMethode', ParametreMethode);

    ParametreMethode.$inject = ['$resource'];

    function ParametreMethode ($resource) {
        var resourceUrl =  'api/parametre-methodes/:id';

        return $resource(resourceUrl, {}, {
               'queryMethode': { method: 'GET', url:'api/parametre-methodes/methode/:idMethode', isArray: true},
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
