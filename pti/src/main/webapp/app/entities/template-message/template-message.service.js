(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('TemplateMessage', TemplateMessage);

    TemplateMessage.$inject = ['$resource'];

    function TemplateMessage ($resource) {
        var resourceUrl =  'api/template-messages/:id';

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
