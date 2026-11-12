(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('ParametreSchemaDetailController', ParametreSchemaDetailController);

    ParametreSchemaDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'ParametreSchema', 'Methode'];

    function ParametreSchemaDetailController($scope, $rootScope, $stateParams, previousState, entity, ParametreSchema, Methode) {
        var vm = this;

        vm.parametreSchema= entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('omnicanalApp:parametreSchemaUpdate', function(event, result) {
            vm.parametreSchema = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
