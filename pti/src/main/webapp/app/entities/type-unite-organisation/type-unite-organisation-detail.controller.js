(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypeUniteOrganisationDetailController', TypeUniteOrganisationDetailController);

    TypeUniteOrganisationDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'TypeUniteOrganisation', 'TableauFrais', 'UniteOrganisation'];

    function TypeUniteOrganisationDetailController($scope, $rootScope, $stateParams, previousState, entity, TypeUniteOrganisation, TableauFrais, UniteOrganisation) {
        var vm = this;

        vm.typeUniteOrganisation = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:typeUniteOrganisationUpdate', function(event, result) {
            vm.typeUniteOrganisation = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
