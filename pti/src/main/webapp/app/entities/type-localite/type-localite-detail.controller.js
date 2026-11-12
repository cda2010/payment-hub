(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypeLocaliteDetailController', TypeLocaliteDetailController);

    TypeLocaliteDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'TypeLocalite', 'UniteOrganisation'];

    function TypeLocaliteDetailController($scope, $rootScope, $stateParams, previousState, entity, TypeLocalite, UniteOrganisation) {
        var vm = this;

        vm.typeLocalite = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:typeLocaliteUpdate', function(event, result) {
            vm.typeLocalite = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
