(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('UniteOrganisationDetailController', UniteOrganisationDetailController);

    UniteOrganisationDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'UniteOrganisation'];

    function UniteOrganisationDetailController($scope, $rootScope, $stateParams, previousState, entity, UniteOrganisation) {
        var vm = this;

        vm.uniteOrganisation = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('omnicanalApp:uniteOrganisationUpdate', function(event, result) {
            vm.uniteOrganisation = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
